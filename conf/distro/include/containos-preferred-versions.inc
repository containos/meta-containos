# A number of recipes (godep, kubernetes) assume go >=1.6 for ./vendor/
# Go 1.10 shared runtime is broken: https://github.com/golang/go/issues/24640
GOVERSION = "1.9%"
