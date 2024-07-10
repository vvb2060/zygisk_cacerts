APP_CFLAGS       := -Wall -Wextra
APP_CPPFLAGS     := -std=c++2b
APP_CONLYFLAGS   := -std=c2x
APP_STL          := none
APP_THIN_ARCHIVE := true
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true

ifneq ($(NDK_DEBUG),1)
APP_CFLAGS       += -Werror -Os -flto
APP_CFLAGS       += -fvisibility=hidden -fvisibility-inlines-hidden
APP_CFLAGS       += -fno-unwind-tables -fno-asynchronous-unwind-tables -fno-stack-protector
APP_LDFLAGS      += -Wl,--exclude-libs,ALL -flto
endif
