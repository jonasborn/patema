set -a
sudo echo "Sudo available"
declare -a arr=("script/tapeinfo.sh" "script/sg_logs.sh" "script/lsscsi.sh")

## now loop through the above array
for i in "${arr[@]}"
do
  SCRIPT_PATH=$(realpath $i)
  sudo chmod +x "$SCRIPT_PATH"
  sudo chown root:root "$SCRIPT_PATH"
  ROW="$(whoami) ALL=(ALL) NOPASSWD: $SCRIPT_PATH"
  sudo grep -qxF "$ROW" /etc/sudoers || echo "$ROW" | sudo tee -a /etc/sudoers
done