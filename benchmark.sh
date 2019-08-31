#!/usr/bin/env bash
wrk -t2 -c256 -d10s -T5 --script=./wrk.lua --latency http://127.0.0.1:8087/invoke
wrk -t2 -c256 -d60s -T5 --script=./wrk.lua --latency http://127.0.0.1:8087/invoke
wrk -t2 -c512 -d10s -T5 --script=./wrk.lua --latency http://127.0.0.1:8087/invoke
wrk -t2 -c512 -d60s -T5 --script=./wrk.lua --latency http://127.0.0.1:8087/invoke