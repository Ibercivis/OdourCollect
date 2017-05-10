<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');


if ($_SERVER['REQUEST_METHOD'] == "POST") {

    // Get post data
    $cfa_comment = isset($_POST['cfa_comment']) ? mysql_real_escape_string($_POST['cfa_comment']) : "";
    $report_id = intval($_POST['report_id']);
    $username = isset($_POST['user']) ? mysql_real_escape_string($_POST['user']) : "";

    // Save data into database
    $query = "INSERT INTO cfa_oc (cfa_comment, report_id, user_id) SELECT '$cfa_comment', $report_id, id FROM user_oc WHERE username='$username'";

    $insert = mysql_query($query);

    if($insert){
        // Save data into database
        $query = "SELECT cfa_comment_date FROM cfa_oc WHERE cfa_oc.report_id = $report_id;";
        $result = mysql_query($query);

        while ($posts_row = mysql_fetch_assoc($result))
        {
            $cfa_comment_date = $posts_row['cfa_comment_date'];
        }

        $data = array(
            "result" => 1,
            "cfa_comment" => "$cfa_comment",
            "username" => "$username",
            "cfa_comment_date" => "$cfa_comment_date",
            "message" => "Comment succesfully added!"
        );

    } else {
        $data = array("result" => 0, "message" => "Error! Database error.");
    }
} else {
    $data = array("result" => 0, "message" => "Method not allowed");
    http_response_code(405);
}

mysql_close($conn);
/* JSON Response */
echo json_encode($data);

?>

