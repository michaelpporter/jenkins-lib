# Test Library

Tests seup of a the shared libraries and echos the build number

## Sample Jenkinsfile

```groovy
#!groovy​
library identifier: 'MPP_JENKINS@master',
  retriever: modernSCM(
    github(id: 'MPPJENKINSREPO', repoOwner: 'michaelpporter', repository: 'jenkins-lib'))


pipeline {
  agent any
  options {
      buildDiscarder(logRotator(numToKeepStr: '7'))
  }
  stages {
    stage("Checkout") {
      steps {
          test("this is build #${BUILD_NUMBER}")
      }
    }
  }
}
```

