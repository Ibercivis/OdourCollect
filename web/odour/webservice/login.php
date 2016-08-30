<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');


if($_SERVER['REQUEST_METHOD'] == "POST"){
    // Get post data`
    
    $username = isset($_POST['username']) ? mysql_real_escape_string($_POST['username']) : "";
    $password = isset($_POST['password']) ? mysql_real_escape_string($_POST['password']) : "";
    
    // Save data into database
    $query = "SELECT * FROM `user_oc` WHERE username='$username';";
    $result = mysql_query($query);
    
    while ($posts_row = mysql_fetch_assoc($result))
    {   
        $md5_password = md5($password);
        if ($md5_password == $posts_row['password']) {
            $data = array("result" => 1);
        } else {
            $data = array("result" => 0, "message" => "Error! Username and password do not match.");
        }
    }

    if(mysql_num_rows($result) != 1){
        $data = array("result" => 0, "message" => "Wrong username");
    }
}

mysql_close($conn);
/* JSON Response */
echo json_encode($data);


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

