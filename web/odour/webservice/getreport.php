<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');

    // Get post data`
    
    $report_id = intval($_POST['report_id']);
    
    // Save data into database
    $query = "SELECT comment, comment_date, user_oc.username FROM user_oc, comment_oc WHERE user_oc.id = user_id and comment_oc.report_id = $report_id;";
    $result = mysql_query($query);
    
    $posts_array = array();

    while ($posts_row = mysql_fetch_assoc($result))
    {
        array_push($posts_array, $posts_row);
    }
    
    // Save data into database
    $query = "SELECT $report_id, annoyance, cloud, duration, intensity, latitude, longitude, origin, pic, rain, wind, report_date, type, user_oc.username AS username FROM user_oc, report_oc WHERE report_oc.id=$report_id and user_oc.id = report_oc.user_id;";
    $result = mysql_query($query);
    
    while ($posts_row = mysql_fetch_assoc($result))
    {   
        $username = $posts_row['username'];
        $annoyance = $posts_row['annoyance'];
        $cloud = $posts_row['cloud'];
        $duration = $posts_row['duration'];
        $intensity = $posts_row['intensity'];
        $latitude = $posts_row['latitude'];
        $longitude = $posts_row['longitude'];
        $origin = $posts_row['origin'];
        $pic = $posts_row['pic'];
        $rain = $posts_row['rain'];
        $wind = $posts_row['wind'];
        $report_date = $posts_row['report_date'];
        $type = $posts_row['type'];
    }
    
    $report_complete = array("result" => 1, "username" => "$username", "annoyance" => "$annoyance", "cloud" => "$cloud", "duration" => "$duration", "intensity" => "$intensity", "latitude" => "$latitude", "longitude" => "$longitude", "origin" => "$origin", "pic" => $pic, "rain" => "$rain", "wind" => "$wind", "report_date" => "$report_date", "type" => "$type", "report_id" => "$report_id", "comments" => json_encode($posts_array));
    

mysql_close($conn);
/* JSON Response */
echo json_encode($report_complete);


/*$posts_array = array();

while ($posts_row = mysqli_fetch_array($run_posts))
{
  $row_array['id'] = $posts_row['id'];
  $row_array['game'] = $posts_row['game'];
  $row_array['date'] = $posts_row['date'];
  array_push($posts_array, $row_array);
}

$string = json_encode($posts_array, JSON_UNESCAPED_UNICODE);
echo $string;

*/

?>
