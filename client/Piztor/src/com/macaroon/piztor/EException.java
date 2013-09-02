package com.macaroon.piztor;



public class EException extends Exception {
	
	static final int EConnectedFailedException =101;
	static final int ETimeOutException =102;
	static final int EJavaHostException =103;
	static final int EPushFailedException =104;
	static final int EIOException =105;
	static final int EUnknownHostException =106;
	static final int EStatusFailedException =107;	
	static final int ELevelFailedException =108;
	static final int EPasswordFailedException =109;
	static final int ESubscribeFailedException =110;
	static final int ECheckinFailedException =111;
	
	private static final long serialVersionUID = 100L;
		int Rtype;
		int Etype;
		long time;
		public EException(int e,int r,long timep) {  
			super();
			Rtype = r;
			Etype = e;
			time = timep;
		}
	}
	
	class EConnectFailedException extends EException{
		private static final long serialVersionUID = 101L;
		public EConnectFailedException(int t,long timep) {  
			super(101,t,timep);
			}		
	}
	
	class ETimeOutException extends EException{
		private static final long serialVersionUID = 102L;
		public ETimeOutException(int t,long timep) {  
			super(102,t,timep);
			}		
	}
	
	class EJavaHostException extends EException{
		private static final long serialVersionUID = 103L;
		public EJavaHostException(int t,long timep) {  
			super(103,t,timep);
		}		
	}
	
	class EPushFailedException extends EException{
		private static final long serialVersionUID = 104L;
		public EPushFailedException(int t,long timep) {  
			super(104,t,timep);
		}		
	}
	
	class EIOException extends EException{
		private static final long serialVersionUID = 105L;
		public EIOException(int t,long timep) {  
			super(105,t,timep);
		}		
	}	
	
	class EUnknownHostException extends EException{
		private static final long serialVersionUID = 106L;
		public EUnknownHostException(int t,long timep) {  
			super(106,t,timep);
		}		
	}	
	
	class EStatusFailedException extends EException{
		private static final long serialVersionUID = 107L;
		public EStatusFailedException(int t,long timep) {  
			super(107,t,timep);
		}		
	}	
	
	class ELevelFailedException extends EException{
		private static final long serialVersionUID = 108L;
		public ELevelFailedException(int t,long timep) {  
			super(108,t,timep);
		}		
	}	
	
	class EPasswordFailedException extends EException{
		private static final long serialVersionUID = 109L;
		public EPasswordFailedException(int t,long timep) {  
			super(109,t,timep);
		}		
	}
	
	class ESubscribeFailedException extends EException{
		private static final long serialVersionUID = 110L;
		public ESubscribeFailedException(int t,long timep) {  
			super(110,t,timep);
		}		
	}
	
	class ECheckinFailedException extends EException{
		private static final long serialVersionUID = 111L;
		public ECheckinFailedException(int t,long timep) {  
			super(111,t,timep);
		}		
	}