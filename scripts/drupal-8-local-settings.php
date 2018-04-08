<?php

$$databases['default']['default'] = array(
  'database' => '${DATABASE_NAME}',
  'username' => '${DATABASE_USER}',
  'password' => '${DATABASE_PASS}',
  'prefix' => '',
  'host' => 'localhost',
  'port' => '3306',
  'namespace' => 'Drupal\\Core\\Database\\Driver\\mysql',
  'driver' => 'mysql',
);

$$settings['hash_salt'] = 'billydontloosemynumber';
$$config_directories[CONFIG_SYNC_DIRECTORY] = '../config/sync';

@include('settings.stage.php');

$$settings['trusted_host_patterns'] = array(
  '^${SITE_NAME}\.xenostaging\.com$$',
);
