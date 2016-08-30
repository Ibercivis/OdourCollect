<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');


if($_SERVER['REQUEST_METHOD'] == "POST"){

    // Get post data
    $comment = isset($_POST['comment']) ? mysql_real_escape_string($_POST['comment']) : "";
    $report_id = intval($_POST['report_id']);
    $username = isset($_POST['user']) ? mysql_real_escape_string($_POST['user']) : "";
    
    // Save data into database
    $query = "INSERT INTO comment_oc (comment, report_id, user_id) SELECT '$comment', $report_id, id FROM user_oc WHERE username='$username'";
    
    $insert = mysql_query($query);

    if($insert){
        // Save data into database
        $query = "SELECT comment_date FROM comment_oc WHERE comment_oc.report_id = $report_id;";
        $result = mysql_query($query);
        
        while ($posts_row = mysql_fetch_assoc($result))
        {
            $comment_date = $posts_row['comment_date'];
        }
        
        $data = array("result" => 1, "comment" => "$comment", "username" => "$username", "comment_date" => "$comment_date", "message" => "Comment succesfully added!");
        
    } else {
        $data = array("result" => 0, "message" => "Error! Database error.");
    }
}

mysql_close($conn);
/* JSON Response */
echo json_encode($data);

?>

