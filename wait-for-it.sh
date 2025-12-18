# wait-for-it.sh
#!/bin/bash
# This script waits until MySQL is up and running.

host="$1"
shift
cmd="$@"

until mysqladmin ping -h"$host" --silent; do
  echo "Waiting for MySQL at $host..."
  sleep 2
done

echo "MySQL is up!"
exec $cmd
