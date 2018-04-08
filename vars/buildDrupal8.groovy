/**
 * Build Test
 *
 * @param siteName Folder to create for the site
 * @param dbBackup path to a database back on the server
 * @param dbUser User to create for the site
 * @param liveDomain Live Domain for Stage File Proxy
 * @param buildLabel If you are using Jenkins Slaves, which server to run the build on.
 * @param config Drush cim commnad, if you are using config split change this to the string you user for import
 */
def call(String siteName,
         String dbBackup,
         String dbUser,
         String liveDomain = 'https://www.example.com',
         String buildLabel = 'master',
         String config = 'cim') {
    echo "BuildD8  - Files"
    build job: 'global-builds/drupal-8-settings', 
        parameters: [
            string(name: 'SITE_NAME', value: "${siteName}"), 
            string(name: 'DATABASE_NAME', value: "${siteName.replaceAll('-','_')}"), 
            string(name: 'DATABASE_USER', value: "${dbUser}"), 
            string(name: 'DATABASE_PASS', value: "${dbUser}"), 
            [$class: 'LabelParameterValue', name: 'node', label: "${buildLabel}"]]
    echo "BuildD8  - Shell"
    sh """#!/bin/bash
        echo \"#!/bin/bash\" > /var/jenkins-sites/${siteName}.txt
        echo \"cd /var/www/${siteName}/web\" >> /var/jenkins-sites/${siteName}.txt
        echo \"sudo fdperms\" >> /var/jenkins-sites/${siteName}.txt
        echo \"drush sql-query 'DROP DATABASE IF EXISTS ${siteName.replaceAll('-','_')};'\" >> /var/jenkins-sites/${siteName}.txt
        echo \"cd /var/www/\" >> /var/jenkins-sites/${siteName}.txt
        echo \"rm -rf /var/www/${siteName}\" >> /var/jenkins-sites/${siteName}.txt
        echo \"rm -rf /var/www/${siteName}@*\" >> /var/jenkins-sites/${siteName}.txt
        NUMBER="\$(doctl compute domain records list xenostaging.com | grep '\\s${siteName}\\s' | awk '{print \$1;}')"
        echo \"doctl compute domain records delete xenostaging.com \$NUMBER -f\" >> /var/jenkins-sites/${siteName}.txt
        echo \"echo cleanup complete\" >> /var/jenkins-sites/${siteName}.txt
        echo \"rm -- /var/jenkins-sites/${siteName}.txt\" >> /var/jenkins-sites/${siteName}.txt
        chmod +x /var/jenkins-sites/${siteName}.txt
        composer install
        cd web
        mkdir -p sites/default/files/private
        sudo fdperms
        newmysqlsingle ${siteName.replaceAll('-','_')} ${dbUser}
        cp /var/livedb/${dbBackup} db.sql.gz
        gunzip db.sql.gz
        drush sql-cli < db.sql
        rm -f db.sql
        drush updb -y
        drush ${config} -y
        drush pm-enable --yes stage_file_proxy
        drush config-set stage_file_proxy.settings origin \"${liveDomain}\"
        drush cr -y
        """
    return "${siteName.replaceAll('-','_')}"

}
