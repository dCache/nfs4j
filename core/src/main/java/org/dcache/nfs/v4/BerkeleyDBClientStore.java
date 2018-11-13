/*
 * Copyright (c) 2018 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.nfs.v4;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;

import org.dcache.nfs.status.NoGraceException;
import org.dcache.nfs.status.ReclaimBadException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An implementation of {@link ClientRecoveryStore} which uses BerkeleyDB-JE to
 * store client records.
 *
 * <p> On the start the previously existing db will be used as recovery DB while
 * a fresh database for new clients will be created. After recover is complete, due
 * to grace period expiry or when all known clients have complete the recovery, the
 * old db will be removed and new one will take it's place.
 *
 * @since 0.18
 */
public class BerkeleyDBClientStore implements ClientRecoveryStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(BerkeleyDBClientStore.class);

    /**
     * Client db name.
     */
    private static final String CLIENT_DB = "nfs-client-db";

    /**
     * Client db name during recovery period.
     */
    private static final String CLIENT_DB_RECOVER = "nfs-client-db.new";

    private final Environment env;

    /**
     * Database with actual client records.
     */
    private Database clientDatabase;

    /**
     * Database with records from before reboot.
     */
    private Database clientRecoveryDatabase;

    private final DatabaseConfig dbConfig;

    private final CursorConfig config = new CursorConfig();

    private final Instant bootTime = Instant.now();

    public BerkeleyDBClientStore(File dir) {

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        envConfig.setReadOnly(false);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        env = new Environment(dir, envConfig);

        dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);
        dbConfig.setReadOnly(false);

        config.setReadCommitted(true);

        // initially we have them swaped to use old db for recovery
        clientRecoveryDatabase = env.openDatabase(null, CLIENT_DB, dbConfig);
        clientDatabase = env.openDatabase(null, CLIENT_DB_RECOVER, dbConfig);

        /**
         * if there are entries in the CLIENT_DB_RECOVER, then we hit the reboot
         * during recovery. Copy over into client DB before we use it for recovery.
         */
        Transaction tx = env.beginTransaction(null, null);
        try (Cursor cursor = clientDatabase.openCursor(tx, config)) {

            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();

            while (cursor.getNext(key, data, null) == OperationStatus.KEYEMPTY.SUCCESS) {
                clientRecoveryDatabase.putNoOverwrite(tx, key, data);
                cursor.delete();
            }

        } finally {
            tx.commit();
        }

        dump();
    }

    /**
     * Add client record into recovery store. An existing record for provided
     * {@code client} will be updated.
     *
     * @param client client's unique identifier.
     */
    @Override
    public synchronized void addClient(byte[] client) {
        Instant now = Instant.now();

        DatabaseEntry key = new DatabaseEntry(client);
        DatabaseEntry data = new DatabaseEntry();

        LongBinding.longToEntry(now.toEpochMilli(), data);

        LOGGER.debug("New client record [{}] at {}", new String(client, UTF_8), now);
        clientDatabase.put(null, key, data);
    }

    /**
     * Remove client record from recovery store. Called when client record is
     * destroyed due to expiry or destroy (unmount).
     *
     * @param client client's unique identifier.
     */
    @Override
    public synchronized void removeClient(byte[] client) {
        DatabaseEntry key = new DatabaseEntry(client);
        clientDatabase.delete(null, key);
        if (clientRecoveryDatabase != null) {
            clientRecoveryDatabase.delete(null, key);
        }
    }

    /**
     * Indicates that {@code owner} have finished reclaim procedure. This method
     * is called by client even it there was no stated to reclaim.
     *
     * @param client client's unique identifier.
     */
    @Override
    public synchronized void reclaimClient(byte[] client) {

        if (clientRecoveryDatabase == null) {
            return;
        }

        DatabaseEntry key = new DatabaseEntry(client);

        LOGGER.debug("Removing recovery record for client [{}]", new String(client, UTF_8));
        clientRecoveryDatabase.delete(null, key);

        // do lazy cleanup
        if (clientRecoveryDatabase.count() == 0) {
            LOGGER.debug("No more client to recover - ending grace period.");
            reclaimComplete();
        }
    }

    /**
     * Check that client is eligible to reclaim states.
     *
     * @param client client's unique identifier.
     *
     * @throws NoGraceException is grace period is over
     * @throws ReclaimBadException client's prevision state can't be detected.
     */
    @Override
    public synchronized void wantReclaim(byte[] client) throws NoGraceException, ReclaimBadException {

        if (clientRecoveryDatabase == null) {
            throw new NoGraceException("Grace period expired");
        }

        DatabaseEntry key = new DatabaseEntry(client);
        DatabaseEntry data = new DatabaseEntry();

        LOGGER.debug("Removing recovery record for client [{}]", new String(client, UTF_8));
        OperationStatus status = clientRecoveryDatabase.get(null, key, data, LockMode.READ_COMMITTED);
        if (status != OperationStatus.SUCCESS) {
            LOGGER.debug("No record for client [{}]", new String(client, UTF_8));
            throw new ReclaimBadException("No pre-reboot record found");
        }

    }

    /**
     * Checks this client store for a pending reclaim. The does not expects any
     * reclaims when grace period is expired or all previously existing clients
     * have complete their reclaims.
     *
     * @return true if store expects reclaims from previously existing clients.
     */
    @Override
    public synchronized boolean waitingForReclaim() {

        if (clientRecoveryDatabase == null) {
            return false;
        }

        Transaction tx = env.beginTransaction(null, null);
        try (Cursor cursor = clientRecoveryDatabase.openCursor(tx, config)) {

            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();

            /*
             * Are there entries from previous instance. If not, the we are ready
             */
            while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS) {
                Instant clientCreationTime = Instant.ofEpochMilli(LongBinding.entryToLong(data));
                if (clientCreationTime.isBefore(bootTime)) {
                    LOGGER.debug("Recovery: wating for client [{}] at {}", new String(key.getData(), UTF_8), clientCreationTime);
                    return true;
                }
            }

            return false;

        } finally {
            tx.commit();
        }

    }

    /**
     * Dump current content of recovery database.
     */
    private void dump() {

        Transaction tx = env.beginTransaction(null, null);

        try (Cursor cursor = clientRecoveryDatabase.openCursor(tx, config)) {

            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();
            /*
             * Are there entries from previous instance. If not, the we are ready
             */
            while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS) {
                Instant clientCreationTime = Instant.ofEpochMilli(LongBinding.entryToLong(data));
                LOGGER.info("NFS client record to recover: [{}], {}", new String(key.getData(), UTF_8), clientCreationTime);
            }
        } finally {
            tx.commit();
        }

    }

    /**
     * Remove all record for client's that did not showed up during grace
     * period. Drop recovery database.
     */
    @Override
    public synchronized void reclaimComplete() {

        if (clientRecoveryDatabase == null) {
            return;
        }

        Transaction tx = env.beginTransaction(null, null);

        try {

            try (Cursor cursor = clientRecoveryDatabase.openCursor(tx, config)) {

                DatabaseEntry key = new DatabaseEntry();
                DatabaseEntry data = new DatabaseEntry();

                while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS) {
                    Instant clientCreationTime = Instant.ofEpochMilli(LongBinding.entryToLong(data));
                    LOGGER.info("Dropping expired recovery record: [{}], {}", new String(key.getData(), UTF_8), clientCreationTime);
                }
            }

            clientDatabase.close();
            clientRecoveryDatabase.close();
            clientRecoveryDatabase = null;

            env.removeDatabase(tx, CLIENT_DB);
            env.renameDatabase(tx, CLIENT_DB_RECOVER, CLIENT_DB);

            clientDatabase = env.openDatabase(tx, CLIENT_DB, dbConfig);
        } finally {
            tx.commit();
        }
    }

    @Override
    public synchronized void close() {
        clientDatabase.close();
        if (clientRecoveryDatabase != null) {
            clientRecoveryDatabase.close();
            clientRecoveryDatabase = null;
        }
    }
}
