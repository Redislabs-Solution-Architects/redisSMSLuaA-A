#!/bin/bash

echo "Creating Redis Enterprise clusters..."
sudo docker exec -it --privileged re-node1 "/opt/redislabs/bin/rladmin" cluster create name cluster1.local username jason.haugland@redislabs.com password jasonrocks

sudo docker exec -it --privileged re-node2 "/opt/redislabs/bin/rladmin" cluster create name cluster2.local username jason.haugland@redislabs.com password jasonrocks

# Test the cluster 1
docker exec -it re-node1 bash -c "/opt/redislabs/bin/rladmin status"
# Test the cluster 2
docker exec -it re-node2 bash -c "/opt/redislabs/bin/rladmin status"
