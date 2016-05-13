# FIXME: this is much harder than it should be, because of the way the
# parent functions are written.

do_configure_append () {
  CONF_SED_SCRIPT=""
  mv ${B}/.config ${B}/.config-base

  kernel_conf_variable OVERLAY_FS y

  kernel_conf_variable NAMESPACES y
  kernel_conf_variable UTS_NS y
  kernel_conf_variable IPC_NS y
  kernel_conf_variable USER_NS y
  kernel_conf_variable PID_NS y
  kernel_conf_variable NET_NS y

  kernel_conf_variable SECCOMP y
  kernel_conf_variable SECCOMP_FILTER y

  sed -e "${CONF_SED_SCRIPT}" \
    < '${B}/.config-base' >>'${B}/.config'

  yes '' | oe_runmake -C ${S} O=${B} oldconfig
}
