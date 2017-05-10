<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');

$query = "
    SELECT
      report_oc.id AS report_id, annoyance, intensity, latitude, longitude, report_date, type, user_oc.username AS username, user_oc.id AS user_id, (
        SELECT COUNT(*) FROM comment_oc WHERE comment_oc.report_id = report_oc.id
      ) AS number_comments
    FROM
      user_oc, report_oc
";

if (isset($_GET['latitude']) && isset($_GET['longitude'])) {
  $latitude = floatval($_GET['latitude']);
  $longitude = floatval($_GET['longitude']);
  $distance = 50; // 50 km

  $query .= "
    JOIN
      (SELECT
        id, (
          6371 * acos (
            cos ( radians($latitude) )
            * cos( radians( latitude ) )
            * cos( radians( longitude ) - radians($longitude) )
            + sin ( radians($latitude) )
            * sin( radians( latitude ) )
          )
        ) AS distance
      FROM report_oc
      HAVING distance < $distance
    ) a
    ON
      report_oc.id = a.id
  ";
}

$query .= "
  WHERE
    report_oc.user_id = user_oc.id
  ORDER BY
    report_date DESC
";

if (isset($_GET['limit'])) {
  $limit = intval($_GET['limit']);
  $query .= "LIMIT $limit";
}

$result = mysql_query($query);

$posts_array = array();

while ($posts_row = mysql_fetch_assoc($result))
{
    array_push($posts_array, $posts_row);
}

mysql_close($conn);

/* JSON Response */
echo json_encode($posts_array);
?>