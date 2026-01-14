APP_CFLAGS       := -Wall -Wextra
APP_CPPFLAGS     := -std=c++23
APP_CONLYFLAGS   := -std=c23
APP_STL          := none
APP_THIN_ARCHIVE := true

ifneq ($(NDK_DEBUG),1)
APP_CFLAGS       += -Werror -Os -flto
APP_CFLAGS       += -fvisibility=hidden -fvisibility-inlines-hidden
APP_CFLAGS       += -fno-unwind-tables -fno-asynchronous-unwind-tables -fno-stack-protector
APP_LDFLAGS      += -Wl,--exclude-libs,ALL -flto
endif
