

#include <iostream>
#include <fstream> 
#include <windows.h> 
#include <conio.h>
#include <tchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include "SerialPort.h"

using std::cout;
using std::endl;


char *port_name = "\\\\.\\COM4";

//String for incoming data
char incomingData[MAX_DATA_LENGTH];


int main()
{
	HANDLE hPipe;
    DWORD dwWritten;
    
    hPipe = CreateNamedPipe(TEXT("\\\\.\\pipe\\Pipe"),
                            PIPE_ACCESS_DUPLEX,
                            PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,   // FILE_FLAG_FIRST_PIPE_INSTANCE is not needed but forces CreateNamedPipe(..) to fail if the pipe already exists...
                            1,
                            1024 * 16,
                            1024 * 16,
                            NMPWAIT_USE_DEFAULT_WAIT,
                            NULL);
                            
	SerialPort arduino(port_name);
	if (arduino.isConnected()) cout << "Connection Established" << endl;
	else cout << "ERROR, check port name";
	while((arduino.isConnected())&&(hPipe != INVALID_HANDLE_VALUE)){
			
		//Check if data has been read or not
		int read_result = arduino.readSerialPort(incomingData, MAX_DATA_LENGTH);
		char* array[20];
		int i =0;
		int j=0;
		char* pch = NULL;
	    pch = strtok(incomingData, "\n");
		if (pch == NULL){
			Sleep(1000);
			continue;
		}
	    while (pch != NULL)
	    {
	    	array[i++]=pch;
	    	
			pch = strtok(NULL, "\n");
	    }
	    if (ConnectNamedPipe(hPipe, NULL) != FALSE)   // wait for someone to connect to the pipe
		{
	    	WriteFile(hPipe,
                  array[1],
                  strlen(array[1]),   
                  &dwWritten,
                  NULL);
        }
        else if (GetLastError() == ERROR_PIPE_CONNECTED) // already connected
        {
        	WriteFile(hPipe,
                  array[1],
                  strlen(array[1]),   
                  &dwWritten,
                  NULL);
		}
		cout << array[1];
		cout << "\n";
	    memset(incomingData, 0, sizeof incomingData);
	    memset(array, 0, sizeof array);
	    Sleep(1000);
	}
		
	puts("Disconnected");
	Sleep(10000);
	CloseHandle(hPipe);
		
}
