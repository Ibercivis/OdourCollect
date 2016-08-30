<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');

    
    // Save data into database
    $query = "select `report_oc`.`id` AS report_id, annoyance, intensity, latitude, longitude, report_date, type, `user_oc`.`username` as user, `user_oc`.`id` as user_id, (SELECT COUNT(*) FROM comment_oc WHERE comment_oc.report_id = report_oc.id) AS number_comments  from user_oc, report_oc where report_oc.user_id = user_oc.id;";
    $result = mysql_query($query);
    
    $posts_array = array();

    while ($posts_row = mysql_fetch_assoc($result))
    {
        array_push($posts_array, $posts_row);
    }

mysql_close($conn);
/* JSON Response */
echo json_encode($posts_array);


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
