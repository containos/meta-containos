inherit gettext

# attr ./configure uses --disable-gettext rather than usual --disable-nls
EXTRA_OECONF += "${@'--disable-gettext' if d.getVar('USE_NLS', True) == 'no' else '--enable-gettext'}"
