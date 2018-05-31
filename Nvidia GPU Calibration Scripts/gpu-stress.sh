#!/bin/bash
CORECOUNT=$1

if [ "$CORECOUNT" == "" ]; then
  echo "First Argument should be the core count, exiting..."
  exit 1
fi

#!/bin/bash
INCREMENT=1
RUN=1
MAXLOOPCOUNT=20
while [ $RUN -le $MAXLOOPCOUNT ]; do
echo "Running iteration:" $CORECOUNT
    ./stress $CORECOUNT 256;
    RUN=$((RUN + INCREMENT));
done
