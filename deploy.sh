set ver $argv[1]
set zip_file "neolog-$ver.zip"
echo "version is $ver"
scp -i ~/.ssh/kelly "./target/universal/$zip_file" kelly@neolog.local:/home/kelly/Desktop/neolog
ssh -i ~/.ssh/kelly kelly@neolog.local "source ~/Desktop/neolog/deploy_server.sh neolog-$ver"
