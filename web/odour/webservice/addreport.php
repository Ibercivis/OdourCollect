<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');


if($_SERVER['REQUEST_METHOD'] == "POST"){

    // Get post data
    $type = $_POST['type']=='Other' ? mysql_real_escape_string($_POST['other_type']) : mysql_real_escape_string($_POST['type']);
    $origin = isset($_POST['origin']) ? mysql_real_escape_string($_POST['origin']) : "";
    $annoyance = intval($_POST['annoyance']);
    $cloud = intval($_POST['cloud']);
    $duration = isset($_POST['duration']) ? mysql_real_escape_string($_POST['duration']) : "";
    $intensity = intval($_POST['intensity']);
    
    $latlng = isset($_POST['latlng']) ? mysql_real_escape_string($_POST['latlng']) : "";
    
    $tokenBegin   = '(';
    $tokenEnd   = ',';
    $posBegin = strpos($latlng, $tokenBegin);
    $posEnd = strpos($latlng, $tokenEnd);
    $latitude = substr($latlng, $posBegin+1, $posEnd-$posBegin);
    $latitude = (float) $latitude;
    $tokenBegin   = ' ';
    $tokenEnd   = ')';
    $posBegin = strpos($latlng, $tokenBegin);
    $posEnd = strpos($latlng, $tokenEnd);
    $longitude = substr($latlng, $posBegin+1, $posEnd-$posBegin);
    $longitude = (float) $longitude;
    
    $rain = intval($_POST['rain']);
    $wind = intval($_POST['wind']);
    $username = isset($_POST['user']) ? mysql_real_escape_string($_POST['user']) : "";
    
    // Save data into database
    $query = "INSERT INTO report_oc (type, origin, annoyance, cloud, duration, intensity, latitude, longitude, latlng, rain, wind, user_id) SELECT '$type', '$origin', $annoyance, $cloud, '$duration', $intensity, $latitude, $longitude, '$latlng', $rain, $wind, id FROM user_oc WHERE username='$username'";
    $insert = mysql_query($query);

    if($insert){
        $report_id = mysql_insert_id();
        
        $query = "SELECT report_date, (SELECT COUNT(*) FROM comment_oc WHERE comment_oc.report_id = $report_id) AS number_comments FROM  report_oc WHERE id = $report_id;";
        $result = mysql_query($query);
        
        $posts_array = array();

        while ($posts_row = mysql_fetch_assoc($result))
        {
            $report_date = $posts_row['report_date'];
            $number_comments = $posts_row['number_comments'];
        }

        
        $data = array("result" => 1, "message" => "Successfully user added!", "type" => "$type", "origin" => "$origin", "annoyance" => "$annoyance", "cloud" => "$cloud", "duration" => "$duration", "intensity" => "$intensity", "latlng" => "$latlng", "rain" => "$rain", "wind" => "$wind", "user" => "$username", "report_date" => "$report_date", "number_comments" => "$number_comments" );
    } else {
        $data = array("result" => 0, "message" => "Error! Database error.");
    }
}

mysql_close($conn);
/* JSON Response */
echo json_encode($data);

?>

