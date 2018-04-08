# buildDrupal8 Library

Builds a drupal 8 site from composer[^1], creates the database[^2] and imports condfig

| Variable | Usage |
| --- | --- |
| String siteName | Folder to create for the site |
| String dbBackup | path to a database back on the server |
| String dbUser | User to create for the site |
| String liveDomain | Live Domain for Stage File Proxy |
| String buildLabel | If you are using Jenkins Slaves, which server to run the build on. |
| String config = 'cim' | Drush cim commnad, if you are using config split change this to the string you user for import |

## Sample Jenkinsfile

```groovy
#!groovy
// Include Xeno Global Library
library identifier: 'MPP_JENKINS@master', retriever: modernSCM(github(id: 'MPPJENKINSREPO', repoOwner: 'michaelpporter', repository: 'jenkins-lib'))

def getLabel() {
    // Label of the Jenkins slave to run on, master if you only have one esrver
    return "master"
}
// Choose the site name based on git name and if it is a Pull Request or branch.
def getSitename() {
    // Set the project name, I find it best to use git repo name the Jekninsfile is in.
    SITE_NAME = "SITENAME"
    if (env.CHANGE_BRANCH && !env.CHANGE_FORK){
        return "${SITE_NAME}-${env.CHANGE_BRANCH.toLowerCase()}"
    }
    else{
        return "${SITE_NAME}-${env.BRANCH_NAME.toLowerCase()}"
    }
}

pipeline {
  environment {
    // Database backup name
    X_DB_BACKUP = "SITENAME.sql.gz"
    // Database User to use on the testing server
    X_DB_USER = "SITENAME"
    // The live URL, used for stage file proxy and WP find-replace
    X_LIVE_DOMAIN = "https://www.SITENAME.com"
    // Code paths for phpcs checks, space delimited
    X_CODE = "web/modules/custom/"
    // Code paths for phpcs ignore, comma delimited
    X_IGNORE = "*css"
    // Code paths for phpcs ignore, comma delimited
    X_CIM = "cim"
  }
  agent {
    node {
      label "${getLabel()}"
      customWorkspace "/var/www/${getSitename()}"
    }
  }
  options {
      buildDiscarder(logRotator(numToKeepStr: '7'))
      lock("${getLabel()}")
  }
  stages {
    stage("Checkout") {
      steps {
          checkout scm
      }
    }
    stage("Setup") {
      when {
        // Only build if the site is new
        expression {
          return !fileExists("/var/www/${getSitename()}/web/sites/default/settings.local.php")
        }
      }
      steps {
          xenoBuildD8( "${getSitename()}", env.X_DB_BACKUP, env.X_DB_USER, env.X_LIVE_DOMAIN, "${getLabel()}")
      }
    }
    stage("Drush") {
      when {
        // Only build if the site has been built.
        expression {
          return fileExists("/var/www/${getSitename()}/web/sites/default/settings.local.php")
        }
      }
      steps {
          sh """
            cd web
            drush updb -y
            drush ${X_CIM} -y
            drush cr
          """
      }
    }
  }
}

```

[^1]: Assumes you are using dynamic vhosts based on `/var/www/%1/web/` root
[^2]: Requires `newmysqlsingle` script and fdperms
