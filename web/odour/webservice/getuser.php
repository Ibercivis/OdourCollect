<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');

    // Get post data`
    
    $username = isset($_POST['username']) ? mysql_real_escape_string($_POST['username']) : "";
    
    // Save data into database
    $query = "SELECT report_oc.id AS report_id, annoyance, intensity, latitude, longitude, report_date, type, (SELECT COUNT(*) FROM comment_oc WHERE comment_oc.report_id = report_oc.id) AS number_comments FROM user_oc, report_oc WHERE report_oc.user_id = user_oc.id AND user_oc.username = '$username';";
    $result = mysql_query($query);
    
    $posts_array = array();

    while ($posts_row = mysql_fetch_assoc($result))
    {
        array_push($posts_array, $posts_row);
    }
    
    // Save data into database
    $query = "SELECT * FROM user_oc WHERE username='$username';";
    $result = mysql_query($query);
    
    while ($posts_row = mysql_fetch_assoc($result))
    {   
        $email = $posts_row['email'];
        $age = $posts_row['age'];
        $gender = $posts_row['gender'];
        $signup_date = $posts_row['signup_date'];
    }
    
    $user_complete = array("result" => 1, "username" => "$username", "email" => "$email", "age" => "$age", "gender" => "$gender", "signup_date" => "$signup_date", "reports" => json_encode($posts_array));

mysql_close($conn);
/* JSON Response */
echo json_encode($user_complete);


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
