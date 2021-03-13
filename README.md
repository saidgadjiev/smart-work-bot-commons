# any2any-bot
Commons for smart bots

# Horizontal scalability
```shell script
mkdir -p media/downloads
mkdir -p media/uploads
mkdir -p media/temp
```

#Rsync daemon
/etc/rsyncd.scrt:
```text
user:password
```

/etc/rsyncd.conf:
```text
max connections = 10
log file = /var/log/rsyncd.log

[downloads]
    path = media/downloads/
    comment = Public downloads
    uid = user
    gid = user
    auth users = user
    readonly = true
    secrets file = /etc/rsyncd.scrt
    hosts allow = *

[uploads]
    path = media/uploads/
    comment = Public uploads
    uid = user
    gid = user
    readonly = false
    auth users = user
    secrets file = /etc/rsyncd.scrt
    hosts allow = *
```

```text
Allow tcp/873 
ufw allow and server hosting firewall
```

```shell script
sudo chmod 600 cron.d/rsyncd-client.scrt
sudo systemctl restart rsync
```
#Rsync downloads client
```shell script
mkdir -p cron.d
```

cron.d/rsyncd-client.scrt:
```text
pswd
```

cron.d/rsync_downloads.sh:
```shell script
#!/bin/bash

if [ -e rsync_downloads.lock ]
then
  echo "Rsync downloads job already running...exiting"
  exit
fi

touch rsync_downloads.lock

PATH=/etc:/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin

password_file='cron.d/rsyncd-client.scrt'
user='user'
ip='host'
source='downloads'
destination='media/downloads'

for var in $@
do
	rsync -azP --delete --password-file=$password_file rsync://$user@$ip/$source/$var/ $destination/$var
done

rm rsync_downloads.lock
```

crontab -e:
```text
* * * * * sh cron.d/rsync_downloads.sh >> cron.d/rsync-downloads-client.log 2>&1
```

#Rsync uploads client
cron.d/rsyncd.scrt:
```text
pswd
```

cron.d/rsync_uploads.sh:
```shell script
#!/bin/bash

if [ -e rsync_uploads.lock ]
then
  echo "Rsync uploads job already running...exiting"
  exit
fi

touch rsync_uploads.lock

PATH=/etc:/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin

password_file='cron.d/rsyncd-client.scrt'
user='user'
ip='host'
source='media/uploads/'
destination='uploads'

for var in $@
do
      rsync -azP --remove-source-files --password-file=$password_file $source/$var/ rsync://$user@$ip/$destination/$var
done

rm rsync_uploads.lock
```

crontab -e:
```text
* * * * * sh cron.d/rsync_uploads.sh >> cron.d/rsync-uploads-client.log 2>&1
```

```shell script
sudo chmod 600 cron.d/rsyncd-client.scrt
sudo systemctl restart cron
```