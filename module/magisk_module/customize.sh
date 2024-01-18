SKIPUNZIP=1

enforce_install_from_app() {
  if $BOOTMODE; then
    ui_print "- Installing from Magisk app"
  else
    ui_print "*********************************************************"
    ui_print "! Install from recovery is NOT supported"
    ui_print "! Recovery sucks"
    ui_print "! Please install from Magisk"
    abort "*********************************************************"
  fi
}

enforce_install_from_app

ui_print "- Extracting module files"
unzip -o "$ZIPFILE" -x 'META-INF/*' -d $MODPATH >&2

if [ "$API" -lt 34 ]; then
  rm -rf "$MODPATH/zygisk"
fi

REPLACE="/system/etc/security/cacerts"

set_perm_recursive "$MODPATH" 0 0 0755 0644
