#!/bin/bash

# Variables
HOST="185.8.129.201"
USERNAME="ftpuser"
PASSWORD="securePassword123_@"
PORT="21"
PROJECT_PATH="/mnt/d/lsm/Learning-Management-System"
WEBAPP_PATH="$PROJECT_PATH/webapp"
DIST_PATH="$WEBAPP_PATH/dist/"
REMOTE_UPLOAD_PATH="/home/ftpuser/uploads/"
REMOTE_SERVER_PATH="/var/www/learnovify"
SERVICE_USER="www-data"
SERVICE_GROUP="www-data"

# Build the frontend project
echo "Navigating to webapp directory..."
cd "$WEBAPP_PATH" || { echo "Failed to navigate to webapp director!"; exit 1; }

echo "Building the ReactJS frontend project..."
npm install
npm run build || { echo "Build failed!"; exit 1; }

if [ ! -d "$DIST_PATH" ]; then
  echo "Build directory ($DIST_PATH) not found! Exiting..."
  exit 1
fi

# Upload files via FTP
echo "Uploading built files to cloud server..."
lftp -u $USERNAME,$PASSWORD -p $PORT ftp://learnovify.com <<EOF
set ssl:verify-certificate yes
set ssl:ca-file /etc/ssl/certs/fullchain.pem
mirror -R $DIST_PATH uploads
bye
EOF

if [ $? -ne 0 ]; then
  echo "File upload failed!"
  exit 1
fi

# SSH into the server
echo "Deploying on cloud server..."
sshpass -p 'Hh123456' ssh root@$HOST <<EOF
# Remove existing frontend files
sudo rm -rf $REMOTE_SERVER_PATH/*

# Move new files to the server directory
sudo mv $REMOTE_UPLOAD_PATH/* $REMOTE_SERVER_PATH

# Set proper permissions
sudo chmod -R 755 $REMOTE_SERVER_PATH
sudo chown -R $SERVICE_USER:$SERVICE_GROUP $REMOTE_SERVER_PATH

echo "Frontend deployment completed successfully!"
EOF