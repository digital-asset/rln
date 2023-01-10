from subprocess import Popen, PIPE
import sys

if len(sys.argv) < 4:
    print("Usage:")
    print("publish.py <FILEPATH> <TOPIC> <BROKER> <PORT>")
    exit(1)

filepath = sys.argv[1]
topic = sys.argv[2]
broker = sys.argv[3]
port = sys.argv[4]

broker_and_port = "{}:{}".format(broker, port)

with open(filepath) as file:
    lines = file.readlines()
    for index, line in enumerate(lines):
        print("Publishing message number {} to topic {} ({}):\n{}".format(index + 1, topic, broker_and_port, line))
        process = Popen(["kafka-console-producer", "--topic", topic, "--bootstrap-server", broker_and_port], stdout=PIPE, stdin=PIPE, stderr=PIPE)
        output = process.communicate(input=line.encode())[0]
        if output:
            print("Consumer says: {}".format(output))

print("Done.")
