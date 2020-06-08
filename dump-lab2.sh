echo =================================================
echo "Flow Counts S1/S2/S3 \(deduct 2\):"
sudo ovs-ofctl -OOpenFlow13 dump-flows S1 | wc -l
sudo ovs-ofctl -OOpenFlow13 dump-flows S2 | wc -l
sudo ovs-ofctl -OOpenFlow13 dump-flows S3 | wc -l

echo "S1 Flows ----------->"
sudo ovs-ofctl -OOpenFlow13 dump-flows S1 | sort -k6
echo "-----------------------------------------------------------"
echo "S2 Flows ----------->"
sudo ovs-ofctl -OOpenFlow13 dump-flows S2 | sort -k6
echo "-----------------------------------------------------------"
echo "S3 Flows ----------->"
sudo ovs-ofctl -OOpenFlow13 dump-flows S3 | sort -k6
echo "-----------------------------------------------------------"

#sort -k6 sortiert nach der sechsten Spalte, d.h. nach in_port
