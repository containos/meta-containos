EXTRA_OECONF += "--with-efi-libdir=${libdir} \
                 --with-efi-ldsdir=${libdir} \
                 --with-efi-includedir=${includedir} \
                "

EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', \
                 '--with-sysvrcnd-path=${sysconfdir}/rc.d --with-sysvinit-path=${sysconfdir}/init.d', \
                 '--with-sysvrcnd-path= --with-sysvinit-path=', d)}"
