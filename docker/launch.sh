#!/bin/sh

#DEFINE COLORS
BLUE="\033[0;34m"
GREEN="\033[0;32m"
NC="\033[0m"

# REQUIRES ENV VARIABLES
# REPO_URL - repository url
# FOLDER - folder in repository
# BRANCH - branch

# Send status update to fifo
# $1 message
update_status() {
    echo -e "${GREEN}$1${NC}"
}

# fetch git directory
# $1 = repo url
# $2 = branch
fetch_git_repo() {
    rm -rf repo
    git clone https://github.com/$1 repo
    (cd repo && git checkout $2)
}

# Remove old properties
# $1 = properties file
remove_old_configs () {
    sed -i '/datasource/d' $1
    sed -i '/port/d' $1
	sed -i '/loggin/d' $1
}

# Inject custom database connection
# $1 = properties file
# $2 = inject file
inject_configs() {
  echo -e "\n" >> $1
  cat $2 >> $1
}

# Inject bd driver
# $1 = build gradle file
update_bd_driver() {
  sed -i "s/runtimeOnly.*/runtimeOnly 'org.postgresql:postgresql'/g" $1
}

##
#
# 1. Download git repo
#
update_status "Downloading repo"
fetch_git_repo $REPO_URL $BRANCH
#
# 2. Fix config
#
update_status "Fixing configs"
PROP_PATH="repo/$FOLDER/src/main/resources/application.properties"
BUILD_PATH="repo/$FOLDER/build.gradle"

remove_old_configs $PROP_PATH
inject_configs $PROP_PATH "inject.properties"

echo -e "Updated configs ${BLUE}"
cat $PROP_PATH
sleep 2
echo -e "${NC} \n\n"

update_bd_driver $BUILD_PATH

echo -e "Updated driver ${BLUE}"
cat $BUILD_PATH
sleep 2
echo -e "${NC}"

#
# 3. Run gradle
#
update_status "Running gradle"
cd "repo/$FOLDER" && gradle bootRun
#
# 3. Finalize
#
update_status "Finished"
