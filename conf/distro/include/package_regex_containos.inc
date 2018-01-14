# package_regex_containos.inc - This file contains data that tracks
# upstream project associated with a given recipe. This list is needed
# for recipes that version information can not be automagically discovered.
# As we automate this checking, this file will slowly be reduced.
#
# This data is used by the auto-upgrade-helper, to use it, you could
# add the following to your conf/local.conf:
#   --------------- snip ---------------
#   INHERIT =+ "distrodata"
#   require conf/distro/include/package_regex_containos.inc
#   --------------- snip ---------------
#
# Fore more details about auto-upgrade-help, please refer to:
#   http://git.yoctoproject.org/cgit/cgit.cgi/auto-upgrade-helper/tree/README
#
# The format is as a bitbake variable override for each recipe
#
#       REGEX_URI_pn-<recipe name> = "recipe_url"
#	 - This is the url used by the package checking system to
#	   get the latest version of the package
#       REGEX_pn-<recipe name> = "package_regex"
#	 - This is the regex the package checking system uses to
#	   parse the page found at REGEX_URI_pn-<recipe name>
#

REGEX_URI_pn-rkt = "https://github.com/coreos/rkt/releases"
REGEX_pn-rkt = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

REGEX_URI_pn-libseccomp = "https://github.com/seccomp/libseccomp/releases"
REGEX_pn-libseccomp = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

REGEX_URI_pn-godep = "https://github.com/tools/godep/releases"
REGEX_pn-godep = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

REGEX_URI_pn-kubernetes = "https://github.com/kubernetes/kubernetes/releases"
REGEX_pn-kubernetes = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

REGEX_URI_pn-docker-containerd = "https://github.com/docker/containerd/releases"
REGEX_pn-docker-containerd = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"

REGEX_URI_pn-docker-runc = "https://github.com/docker/runc/releases"
REGEX_pn-docker-runc = "v(?P<pver>(\d+[\.-_]*)+)\.tar\.gz"
