test -n "${BOOT_ORDER}" || setenv BOOT_ORDER "A B"
test -n "${BOOT_A_LEFT}" || setenv BOOT_A_LEFT 3
test -n "${BOOT_B_LEFT}" || setenv BOOT_B_LEFT 3

setenv default_bootargs console=${console} console=tty1 rootwait panic=10 ${extra}

setenv bootargs
for BOOT_SLOT in "${BOOT_ORDER}"; do
  if test "x${bootargs}" != "x"; then
    # skip remaining slots
  elif test "x${BOOT_SLOT}" = "xA"; then
    if test ${BOOT_A_LEFT} -gt 0; then
      setexpr BOOT_A_LEFT ${BOOT_A_LEFT} - 1
      echo "Found valid slot A, ${BOOT_A_LEFT} attempts remaining"
      setenv distro_rootpart 2
      setenv bootargs "${default_bootargs} root=/dev/mmcblk0p2 rauc.slot=A"
    fi
  elif test "x${BOOT_SLOT}" = "xB"; then
    if test ${BOOT_B_LEFT} -gt 0; then
      setexpr BOOT_B_LEFT ${BOOT_B_LEFT} - 1
      echo "Found valid slot B, ${BOOT_B_LEFT} attempts remaining"
      setenv distro_rootpart 3
      setenv bootargs "${default_bootargs} root=/dev/mmcblk0p3 rauc.slot=B"
    fi
  fi
done

if test -n "${bootargs}"; then
  saveenv
else
  echo "No valid slot found, resetting tries to 3"
  setenv BOOT_A_LEFT 3
  setenv BOOT_B_LEFT 3
  saveenv
  reset
fi

echo "Loading devicetree"

# Load devicetree from uboot partition (distro_bootpart)
load ${devtype} ${devnum}:${distro_bootpart} ${fdt_addr_r} ${fdtfile} ||
load ${devtype} ${devnum}:${distro_bootpart} ${fdt_addr_r} boot/${fdtfile}

echo "Loading kernel"

# Load kernel from root partition (distro_rootpart)
load ${devtype} ${devnum}:${distro_rootpart} ${kernel_addr_r} zImage ||
load ${devtype} ${devnum}:${distro_rootpart} ${kernel_addr_r} boot/zImage ||
load ${devtype} ${devnum}:${distro_rootpart} ${kernel_addr_r} uImage ||
load ${devtype} ${devnum}:${distro_rootpart} ${kernel_addr_r} boot/uImage

echo " Starting kernel"

bootz ${kernel_addr_r} - ${fdt_addr_r} ||
bootm ${kernel_addr_r} - ${fdt_addr_r}
