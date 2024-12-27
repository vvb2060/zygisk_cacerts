MODDIR=${0%/*}
API=$(getprop ro.build.version.sdk)

if [ "$API" -ge 29 ]; then
    chcon -R "u:object_r:system_security_cacerts_file:s0" "$MODDIR/system/etc/security/cacerts"
else
    chcon -R "u:object_r:system_file:s0" "$MODDIR/system/etc/security/cacerts"
fi
