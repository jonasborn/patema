rm -rf build
mkdir build
cd build || exit
cmake ..
make
cd ..