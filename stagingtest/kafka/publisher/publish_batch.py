# Same as publish.py, but avoids the overhead of launching kafka-console-producer for each message

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

with open(filepath,"rb") as file:
    print("Publishing message batch to topic {} ({}):\n".format(topic, broker_and_port))
    process = Popen(["kafka-console-producer", "--topic", topic, "--bootstrap-server", broker_and_port], stdout=PIPE, stdin=PIPE, stderr=PIPE)
    output = process.communicate(input=file.read())
    if output != (b'', b''):
        print("Consumer says: {}".format(output))

print("Done.")
