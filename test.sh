function usage () {
    echo "Usage ./test.sh disable|break"
    exit 1
}

[[ $# == 1 ]] || usage
[[ "$1" == disable ]] || [[ "$1" == break ]] || usage
MODE=$1

# Get Minikube service IP
IP=$(minikube service --url=true readylive)

function query() {
    end=$((SECONDS+$1))

    while [ $SECONDS -lt $end ]; do
        curl -w '\n' "$IP/id"
        sleep 0.1
    done
}

query 2
echo
echo "request to $MODE"
curl -w '\n' "$IP/$MODE"
echo
query 20

