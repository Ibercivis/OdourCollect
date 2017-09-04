<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');
include_once('email_config.php');


if ($_SERVER['REQUEST_METHOD'] == "POST") {

    // Get post data
    $comment = isset($_POST['comment']) ? mysql_real_escape_string($_POST['comment']) : "";
    $report_id = intval($_POST['report_id']);
    $username = isset($_POST['user']) ? mysql_real_escape_string($_POST['user']) : "";

    // Save data into database
    $query = "INSERT INTO comment_oc (comment, report_id, user_id) SELECT '$comment', $report_id, id FROM user_oc WHERE username='$username'";

    $insert = mysql_query($query);

    if ($insert) {
        // Save data into database
        $query = "SELECT comment_date FROM comment_oc WHERE comment_oc.report_id = $report_id;";
        $result = mysql_query($query);

        while ($posts_row = mysql_fetch_assoc($result))
        {
            $comment_date = $posts_row['comment_date'];
        }

        $data = array(
            "result" => 1,
            "comment" => "$comment",
            "username" => "$username",
            "comment_date" => "$comment_date",
            "message" => "Comment succesfully added!"
        );

        $query = "SELECT email FROM user_oc, report_oc WHERE user_oc.id = report_oc.user_id and report_oc.id = $report_id";
        $result = mysql_query($query);
        $user_row = mysql_fetch_row($result);

        $to_email = $user_row[0];
        if($to_email !== '') {
            $subject = 'Someone commented on your report';
            $html_content = 'You report on <a href="https://odourcollect.socientize.eu/">https://odourcollect.socientize.eu/</a> has been commented.<br><br>You can read the comments of your report on the following page: <a href="https://odourcollect.socientize.eu/#!/entry/' . $report_id .'">https://odourcollect.socientize.eu/#!/entry/' . $report_id .'</a>';
            $plain_content = 'You report on https://odourcollect.socientize.eu/ has been commented. You can read the comments of your report on the following page: https://odourcollect.socientize.eu/#!/entry/' . $report_id;
            //$status = send_mail($to_email, $subject, $html_content, $plain_content);
        }

    } else {
        $data = array("result" => 0, "message" => "Error! Database error.");
        http_response_code(400);
    }
} else {
    $data = array("result" => 0, "message" => "Method not allowed");
    http_response_code(405);
}

mysql_close($conn);

/* JSON Response */
echo json_encode($data);
?>

