MODDIR=${0%/*}
chcon -R "u:object_r:system_security_cacerts_file:s0" "$MODDIR/system/etc/security/cacerts"
