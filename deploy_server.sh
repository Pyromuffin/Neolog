set zip_fil $argv[1]

if test -e ~/Desktop/neolog/deploy/RUNNING_PID
  kill -s TERM $(cat ~/Desktop/neolog/deploy/RUNNING_PID)
end

unzip -jf ~/Desktop/neolog/$zip_file ~/Desktop/neolog/deploy
source ~/Desktop/neolog/start.sh
