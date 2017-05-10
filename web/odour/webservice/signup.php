<?php
header('Access-Control-Allow-Origin: *');

include_once('db_config.php');


if ($_SERVER['REQUEST_METHOD'] == "POST") {
    // Get post data`

    $username = isset($_POST['username']) ? mysql_real_escape_string($_POST['username']) : "";
    $email = isset($_POST['email']) ? mysql_real_escape_string($_POST['email']) : "";
    $password = isset($_POST['password']) ? mysql_real_escape_string($_POST['password']) : "";
    $age = isset($_POST['age']) ? mysql_real_escape_string($_POST['age']) : "";
    $gender = isset($_POST['gender']) ? mysql_real_escape_string($_POST['gender']) : "";

    // Create md5 hash of the password
    $password = md5($password);

    // Save data into database
    $query = "INSERT INTO user_oc (username,email,password,age,gender) VALUES ('$username','$email','$password','$age','$gender')";
    $insert = mysql_query($query);

    if($insert){
        $data = array("result" => 1, "message" => "Successfully user added!");
    } else {
        $data = array("result" => 0, "message" => "Error! Username already exists.");
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

