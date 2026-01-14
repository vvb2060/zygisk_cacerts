# zygisk cacerts

Replace local root certificate store with [AOSP](https://android.googlesource.com/platform/system/ca-certificates/+archive/refs/heads/main/files.tar.gz).

## Changelog
### 1.0
1. Android 14 CA certificates.
### 1.1
1. Android 15 CA certificates.
2. riscv64 support.
3. 16k page size support.
### 1.2
1. Android 16 CA certificates.
2. fix selinux context before android 10
