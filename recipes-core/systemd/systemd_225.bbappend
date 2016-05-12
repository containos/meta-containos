PACKAGECONFIG[dbus] = "--enable-dbus,--disable-dbus,dbus,dbus"
PACKAGECONFIG[kmod] = "--enable-kmod,--disable-kmod,kmod,kmod"
PACKAGECONFIG[blkid] = "--enable-blkid,--disable-blkid,util-linux"
PACKAGECONFIG[acl] = "--enable-acl,--disable-acl,acl"
PACKAGECONFIG[smack] = "--enable-smack,--disable-smack,,"
PACKAGECONFIG[gnutls] = "--enable-gnutls,--disable-gnutls,gnutls"
PACKAGECONFIG[binfmt] = "--enable-binfmt,--disable-binfmt,,"
PACKAGECONFIG[vconsole] = "--enable-vconsole,--disable-vconsole,,"
PACKAGECONFIG[quotacheck] = "--enable-quotacheck,--disable-quotacheck,quota,"
PACKAGECONFIG[seccomp] = "--enable-seccomp,--disable-seccomp,libseccomp,"
PACKAGECONFIG[bootchart] = "--enable-bootchart,--disable-bootchart"
PACKAGECONFIG[tmpfiles] = "--enable-tmpfiles,--disable-tmpfiles"
PACKAGECONFIG[sysusers] = "--enable-sysusers,--disable-sysusers"
PACKAGECONFIG[randomseed] = "--enable-randomseed,--disable-randomseed"
PACKAGECONFIG[backlight] = "--enable-backlight,--disable-backlight"
PACKAGECONFIG[rfkill] = "--enable-rfkill,--disable-rfkill"
PACKAGECONFIG[logind] = "--enable-logind,--disable-logind"
PACKAGECONFIG[machined] = "--enable-machined,--disable-machined"
PACKAGECONFIG[timedated] = "--enable-timedated,--disable-timedated"
PACKAGECONFIG[timesyncd] = "--enable-timesyncd,--disable-timesyncd"
PACKAGECONFIG[localed] = "--enable-localed,--disable-localed"
PACKAGECONFIG[coredump] = "--enable-coredump,--disable-coredump"
PACKAGECONFIG[polkit] = "--enable-polkit,--disable-polkit,intltool-native,"
PACKAGECONFIG[efi] = "--enable-efi,--disable-efi"
PACKAGECONFIG[gnuefi] = "--enable-gnuefi,--disable-gnuefi,gnu-efi"
PACKAGECONFIG[myhostname] = "--enable-myhostname,--disable-myhostname"
PACKAGECONFIG[hibernate] = "--enable-hibernate,--disable-hibernate"
PACKAGECONFIG[hwdb] = "--enable-hwdb,--disable-hwdb"
PACKAGECONFIG[importd] = "--enable-importd,--disable-importd"
PACKAGECONFIG[hostnamed] = "--enable-hostnamed,--disable-hostnamed"
PACKAGECONFIG[firstboot] = "--enable-firstboot,--disable-firstboot"

PACKAGECONFIG ??= "xz ldconfig dbus kmod blkid gnutls vconsole tmpfiles logind sysusers randomseed binfmt machined hwdb importd firstboot hostnamed timedated timesyncd bootchart quotacheck smack polkit hibernate \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'pam', 'pam', '', d)} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'xkbcommon', '', d)} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'selinux', '', d)} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'acl', 'acl', '', d)} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'wifi', 'rfkill', '', d)} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'nls', 'localed', '', d)} \
                   ${@bb.utils.contains('MACHINE_FEATURES', 'efi', 'efi', '', d)} \
                   ${@bb.utils.contains('MACHINE_FEATURES', 'screen', 'backlight', '', d)} \
                  "

NSS_PKGCONFIG = ""
NSS_PKGCONFIG_class-target = "myhostname"
PACKAGECONFIG_append_libc-glibc = " ${NSS_PKGCONFIG}"

EXTRA_OECONF += "--with-efi-libdir=${libdir} \
                 --with-efi-ldsdir=${libdir} \
                 --with-efi-includedir=${includedir} \
                "

EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', \
                 '--with-sysvrcnd-path=${sysconfdir}/rc.d --with-sysvinit-path=${sysconfdir}/init.d', \
                 '--with-sysvrcnd-path= --with-sysvinit-path=', d)}"
