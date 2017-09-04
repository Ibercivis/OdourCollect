<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');


$query = "select `report_oc`.`id` AS report_id, annoyance, intensity, latitude, longitude, report_date, type, `user_oc`.`username` as user, `user_oc`.`id` as user_id, (SELECT COUNT(*) FROM comment_oc WHERE comment_oc.report_id = report_oc.id) AS number_comments  from user_oc, report_oc where report_oc.user_id = user_oc.id;";
$result = mysql_query($query);

$posts_array = array();

while ($posts_row = mysql_fetch_assoc($result))
{
    array_push($posts_array, $posts_row);
}

$comments_array = array();

$query = "SELECT `comment_oc`.`comment`, `comment_oc`.`comment_date`, `user_oc`.`username` AS user, `report_oc`.`id` AS report_id FROM comment_oc, report_oc, user_oc WHERE user_oc.id = comment_oc.user_id AND report_oc.id = comment_oc.report_id;";
$result = mysql_query($query);

while ($comments_row = mysql_fetch_assoc($result))
{
	array_push($comments_array, $comments_row);
}

$callforaction_array = array();

$query = "SELECT `cfa_oc`.`cfa_comment`, `cfa_oc`.`cfa_comment_date`, `user_oc`.`username` AS user, `report_oc`.`id` AS report_id FROM cfa_oc, report_oc, user_oc WHERE user_oc.id = cfa_oc.user_id AND report_oc.id = cfa_oc.report_id;";
$result = mysql_query($query);

while ($callforaction_row = mysql_fetch_assoc($result))
{
	array_push($callforaction_array, $callforaction_row);
}

$whole_data = array("result" => 1, "reports" => json_encode($posts_array), "comments" => json_encode($comments_array), "callforaction" => json_encode($callforaction_array));

mysql_close($conn);
/* JSON Response */
echo json_encode($whole_data);
