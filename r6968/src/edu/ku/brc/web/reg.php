<?php
  if ($_POST != ''){

    $cnt = 0;
    foreach (array_keys($_POST) as $p) {
        $cnt++;
    }

    $dateTime =  "date = " . date("y/m/d") ." " . date("H:i:s") . "\n";
    $data = "---------------\n" . $dateTime;
    $data = $data . "ip = " . $_SERVER['REMOTE_ADDR'] . "\n";

    $uTime = microtime(true);
    $data = $data . "reg_number = " . $uTime . "\n";

    if ($cnt == 0) {
	echo "No arguments!<br>";
    } else {
    	foreach (array_keys($_POST) as $p) {
             $data = $data . "$p = $_POST[$p]\n";
    	}
    }

    $myFile = "reg.dat";
    $fh = fopen($myFile, 'a') or die("can't open file");
    fwrite($fh, $data);
    fclose($fh);

    echo "1 " . $uTime . "\n";

  } else {
        echo "0";
  }

?>
