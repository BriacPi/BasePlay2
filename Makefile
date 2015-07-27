# Actions
SBT?=sbt
CP?=cp
CD?=cd
TAR?=tar

# Options
ARCHIVE_ROOT=target/universal/stage
ARCHIVE_OUTPUT?=$(shell pwd)/app.tar.gz
ARCHIVE_ARGS+= --exclude='*.tar.gz'

# Create archive as default target
all: $(ARCHIVE_OUTPUT)

# Create an archive containing the application and its dependencies
$(ARCHIVE_OUTPUT): clean app
	$(CD) $(ARCHIVE_ROOT) && $(TAR) $(ARCHIVE_ARGS) -zcf $(ARCHIVE_OUTPUT) .

# Check application's stability with unit and integration tests
test: app
	$(SBT) test

# Compile the application
app:
	$(SBT) stage

# Clean the project
clean:
	$(SBT) clean

# Phony some rules
.PHONY: all test app clean
