#!/bin/sh
# Init script for {serviceName} service

start() {
    echo "Starting {serviceName}"
    sudo rm {pidFile}
    {startCommand}
}

stop() {
    echo "Stopping {serviceName}"
    pid=$(cat {pidFile})
    sudo kill -9 $pid
}

restart() {
    stop
    start
}

status() {
    pid=$(cat {pidFile})
    echo "{serviceName} is running with pid "$pid
}

case "$1" in
start)
    start
    ;;
stop)
    stop
    ;;
restart)
    restart
    ;;
status)
    status
    ;;
*)
    echo "Usage: $0 {start|stop|restart|status}" >&2
    exit 3
    ;;
esac