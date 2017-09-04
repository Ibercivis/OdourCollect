<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');

if ( ! isset($_GET['user_id']) ) {
    http_response_code(400);
    echo 'Missing parameter: user_id';
    exit();
}

$user_id = intval($_GET['user_id']);

// Reports data
$query = "
    SELECT
        report_oc.id AS report_id, annoyance, intensity, latitude, longitude, report_date, type, (
            SELECT COUNT(*) FROM comment_oc WHERE comment_oc.report_id = report_oc.id
        ) AS number_comments
    FROM
        user_oc, report_oc
    WHERE
        report_oc.user_id = user_oc.id AND user_oc.id = $user_id
";
$result = mysql_query($query);

$posts_array = array();

while ($posts_row = mysql_fetch_assoc($result))
{
    array_push($posts_array, $posts_row);
}

// User data
$query = "SELECT * FROM user_oc WHERE id = $user_id";
$result = mysql_query($query);

while ($posts_row = mysql_fetch_assoc($result))
{
    $username = $posts_row['username'];
    $email = $posts_row['email'];
    $age = $posts_row['age'];
    $gender = $posts_row['gender'];
    $signup_date = $posts_row['signup_date'];
}

$user_complete = array(
    "result" => 1,
    "user_id" => "$user_id",
    "username" => "$username",
    "email" => "$email",
    "age" => "$age",
    "gender" => "$gender",
    "signup_date" => "$signup_date",
    "reports" => json_encode($posts_array)
);


mysql_close($conn);

/* JSON Response */
echo json_encode($user_complete);
?>
