       typedef component4     xattrkey4;
       typedef opaque         xattrvalue4<>;
    struct GETXATTR4args {
            /* CURRENT_FH: file */
            xattrkey4     gxa_name;
    };
    union GETXATTR4res switch (nfsstat4 gxr_status) {
     case NFS4_OK:
            xattrvalue4   gxr_value;
     default:
            void;
    };
    enum setxattr_option4 {
            SETXATTR4_EITHER      = 0,
            SETXATTR4_CREATE      = 1,
            SETXATTR4_REPLACE     = 2
    };
    struct SETXATTR4args {
            /* CURRENT_FH: file */
            setxattr_option4 sxa_option;
            xattrkey4        sxa_key;
            xattrvalue4      sxa_value;
    };
    union SETXATTR4res switch (nfsstat4 sxr_status) {
     case NFS4_OK:
            change_info4      sxr_info;
     default:
            void;
    };
    struct LISTXATTRS4args {
            /* CURRENT_FH: file */
            nfs_cookie4    lxa_cookie;
            count4         lxa_maxcount;
    };
    struct LISTXATTRS4resok {
            nfs_cookie4    lxr_cookie;
            xattrkey4      lxr_names<>;
            bool           lxr_eof;
    };
    union LISTXATTRS4res switch (nfsstat4 lxr_status) {
     case NFS4_OK:
            LISTXATTRS4resok  lxr_value;
     default:
            void;
    };
    struct REMOVEXATTR4args {
            /* CURRENT_FH: file */
            xattrkey4      rxa_name;
    };
    union REMOVEXATTR4res switch (nfsstat4 rxr_status) {
     case NFS4_OK:
            change_info4      rxr_info;
     default:
            void;
    };
    /*
     * ACCESS - Check Access Rights
     */
    const ACCESS4_XAREAD    = 0x00000040;
    const ACCESS4_XAWRITE   = 0x00000080;
    const ACCESS4_XALIST    = 0x00000100;
    /*
     * New NFSv4 attribute
     */
    typedef bool            fattr4_xattr_support;
    /*
     * New NFSv4 operations
     */
    /* Following lines are to be added to enum nfs_opnum4 */
    /*
    OP_GETXATTR                = 72,
    OP_SETXATTR                = 73,
    OP_LISTXATTRS              = 74,
    OP_REMOVEXATTR             = 75,
    */
    /*
     * New cases for Operation arrays
     */
    /* Following lines are to be added to nfs_argop4 */
    /*
    case OP_GETXATTR:      GETXATTR4args opgetxattr;
    case OP_SETXATTR:      SETXATTR4args opsetxattr;
    case OP_LISTXATTRS:    LISTXATTRS4args oplistxattrs;
    case OP_REMOVEXATTR:   REMOVEXATTR4args opremovexattr;
    */
    /* Following lines are to be added to nfs_resop4 */
    /*
    case OP_GETXATTR:      GETXATTR4res opgetxattr;
    case OP_SETXATTR:      SETXATTR4res opsetxattr;
    case OP_LISTXATTRS:    LISTXATTRS4res oplistxattrs;
    case OP_REMOVEXATTR:   REMOVEXATTR4res opremovexattr;
    */
