 zip_file=$1

if [ -f ~/Desktop/neolog/deploy/RUNNING_PID ]; then
  kill -s TERM $(cat ~/Desktop/neolog/deploy/RUNNING_PID)
fi

echo "removing old deploy"
rm -rf ~/Desktop/neolog/deploy

echo "unzipping"
unzip  ~/Desktop/neolog/$zip_file.zip -d ~/Desktop/neolog/

echo "renaming"
mv  ~/Desktop/neolog/$zip_file ~/Desktop/neolog/deploy

echo "creating symlinks"
ln -s ~/Desktop/neolog/test_db.mv.db ~/Desktop/neolog/deploy/test_db.mv.db
ln -s ~/Desktop/neolog/test_db.trace.db ~/Desktop/neolog/deploy/test_db.trace.db
