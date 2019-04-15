{\rtf1\ansi\ansicpg1254\cocoartf1671\cocoasubrtf200
{\fonttbl\f0\fswiss\fcharset0 Helvetica;\f1\fnil\fcharset0 Menlo-Bold;\f2\fnil\fcharset0 Menlo-Regular;
\f3\fnil\fcharset0 Menlo-Italic;}
{\colortbl;\red255\green255\blue255;\red0\green0\blue109;\red0\green0\blue254;\red109\green109\blue109;
}
{\*\expandedcolortbl;;\csgenericrgb\c0\c0\c42745;\csgenericrgb\c0\c0\c99608;\csgenericrgb\c42745\c42745\c42745;
}
\paperw11900\paperh16840\margl1440\margr1440\vieww17600\viewh13680\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 \
\
\

\fs72 B\uc0\u304 L 452 PROJE \
\

\fs24 \

\fs36 README.TXT:\
\

\fs28 \

\fs36 \ul Nas\uc0\u305 l \'c7al\u305 \u351 t\u305 rmal\u305 y\u305 z ve Test A\u351 amas\u305  i\'e7in Gereklilikler
\fs28 \ulnone \
\
Kodu javac *.java kullanarak derleyip daha sonra TestServer,TestClient \
classlar\uc0\u305 n\u305  kullanarak test edebilirsiniz.Program\u305  \'e7al\u305 \u351 t\u305 r\u305 rken ve test ederken\
d\'f6k\'fcmanda bize arg\'fcman olarak verilmi\uc0\u351  olmas\u305 na kar\u351 \u305 n test etme a\u351 amas\u305 nda\
s\'fcrekli port numaralar\uc0\u305  girmek yerine bunlar\u305  sabit de\u287 erler olarak 4568,4569 kabul ettik.Test i\u351 lemi s\u305 ras\u305 nda iki tane farkl\u305  terminal a\'e7\u305 p TestServer ve TestClient classlar\u305 n\u305  \'e7al\u305 \u351 t\u305 rd\u305 k.\
\
\

\fs36 \ul Classlar ve Metotlar Hakk\uc0\u305 nda\ulnone \

\fs28 \
Kodda bulunan bir metot hakk\uc0\u305 nda:\
RDTSegment.java class\uc0\u305 nda bulunan computeCheckSum() adl\u305  metot i\'e7in ders slaytlar\u305 n\u305  ve internetteki kaynaklar\u305  inceledik.\
Ancak metodu implementasyonu s\uc0\u305 ras\u305 nda zorluklar ya\u351 ad\u305 k.\u304 nternette di\u287 er kodlar\u305  inceleyerek bu metodun nas\u305 l implement edilece\u287 i\
konusunda bilgi ald\uc0\u305 k.\
\
Kodun \'e7ok b\'fcy\'fck bir k\uc0\u305 sm\u305 n\u305  RDT class\u305  olu\u351 turmaktad\u305 r.Gerekli implementasyonlar bu k\u305 s\u305 mda yap\u305 ld\u305 .Bu class kendi i\'e7inde RDTBuffer,ReceiverThread inner classlar\u305 n\u305  bar\u305 nd\u305 rmaktad\u305 r.\
\
TimeoutHandler class\uc0\u305  sadece SR(Selective Repeat) k\u305 sm\u305  i\'e7in \
kullan\uc0\u305 ld\u305 .\u304 mplementasyon s\u305 ras\u305 nda Go-Back-N i\'e7in sadece bir tane\
zamanlay\uc0\u305 c\u305  tutmak kodun ak\u305 \u351 \u305  ve anlamland\u305 rma a\'e7\u305 s\u305 nda daha uygun g\'f6r\'fcld\'fc.\
\
ReceiverThread.java class\uc0\u305 nda network ten paketleri al\u305 r ve mesajlar\u305  bir \'fcst katmana\
ileterek uygun mesaj\uc0\u305  d\'f6nmesini sa\u287 lar.\
\
RDTBuffer class\uc0\u305  asl\u305 nda buffer\u305  temsil etmektedir.Networkden al\u305 nan \
segmentleri(Transport Layer i\'e7in) saklamak i\'e7in kullan\uc0\u305 l\u305 r.RDT class\u305 nda kullan\u305 lmaktad\u305 r.\
\
\

\fs36 \ul BUG VE \uc0\u304 SSUELAR HAKKINDA
\fs28 \ulnone \
\
1) D\'f6nem s\uc0\u305 k\u305 \u351 \u305 kl\u305 \u287 \u305 ndan dolay\u305  teslime yak\u305 n bir tarihte projeye ba\u351 lad\u305 k.Bitirdi\u287 imizden itibaren a\u351 a\u287 \u305 da size verdi\u287 imiz test caseler d\u305 \u351 \u305 nda \
farkl\uc0\u305  durumlar\u305  g\'f6z \'f6n\'fcne alma \u351 ans\u305 m\u305 z olmad\u305 .Test etti\u287 imiz kadar\u305 yla kodda\
bir bug bulunmamaktad\uc0\u305 r.\
\
TestServer i\'e7in \
\
GBN i\'e7in uzun mesaj ve loss rate olmadan\
\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f1\b\fs24 \cf2 int 
\f2\b0 \cf0 bufsize = \cf3 3\cf0 ;\
RDT rdt = 
\f1\b \cf2 new 
\f2\b0 \cf0 RDT(hostname, dst_port, local_port, bufsize, bufsize);\
RDT.setMSS(\cf3 10\cf0 );\
RDT.setLossRate(\cf3 0\cf0 );\
\
SR i\'e7in loss rate olmadan\
\

\f1\b \cf2 int 
\f2\b0 \cf0 bufsize = \cf3 10\cf0 ;\
RDT rdt = 
\f1\b \cf2 new 
\f2\b0 \cf0 RDT(hostname, dst_port, local_port, bufsize, bufsize);\
RDT.setMSS(\cf3 10\cf0 );\
RDT.setLossRate(\cf3 0\cf0 );\
RDT.protocol = SR;\
\
TestClient i\'e7in\
\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs28 \cf0 GBN i\'e7in uzun mesaj ve loss rate olmadan
\f2\fs24 \
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 \

\f1\b \cf2 int 
\f2\b0 \cf0 messageSize = \cf3 45\cf0 ;\

\f1\b \cf2 int 
\f2\b0 \cf0 bufsize = \cf3 3\cf0 ;\

\f1\b \cf2 byte
\f2\b0 \cf0 [] data = 
\f1\b \cf2 new byte
\f2\b0 \cf0 [messageSize];\
\
RDT rdt = 
\f1\b \cf2 new 
\f2\b0 \cf0 RDT(hostname, dst_port, local_port, bufsize, bufsize);\
RDT.setMSS(\cf3 10\cf0 );\
RDT.setLossRate(\cf3 0\cf0 );\
\

\f1\b \cf2 int 
\f2\b0 \cf0 index = \cf3 1\cf0 ;\
\

\f1\b \cf2 for 
\f2\b0 \cf0 (
\f1\b \cf2 int 
\f2\b0 \cf0 i = \cf3 0\cf0 ; i < messageSize; i++) \{\
    
\f1\b \cf2 if 
\f2\b0 \cf0 ((i % \cf3 10\cf0 ) == \cf3 0\cf0 ) \{\
        data[i] = (
\f1\b \cf2 byte
\f2\b0 \cf0 ) index++;\
    \}\
    
\f1\b \cf2 else 
\f2\b0 \cf0 \{\
        data[i] = (
\f1\b \cf2 byte
\f2\b0 \cf0 ) \cf3 0\cf0 ;\
    \}\
\}\
rdt.send(data, messageSize);\
\
\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 SR i\'e7in loss rate olmadan\
\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f1\b \cf2 int 
\f2\b0 \cf0 messageSize = \cf3 10\cf0 ;\

\f1\b \cf2 int 
\f2\b0 \cf0 bufsize = \cf3 10\cf0 ;\

\f1\b \cf2 int 
\f2\b0 \cf0 numMessages = \cf3 6\cf0 ;\

\f1\b \cf2 byte
\f2\b0 \cf0 [] data = 
\f1\b \cf2 new byte
\f2\b0 \cf0 [messageSize];\
\
RDT rdt = 
\f1\b \cf2 new 
\f2\b0 \cf0 RDT(hostname, dst_port, local_port, bufsize, bufsize);\
RDT.setMSS(\cf3 10\cf0 );\
RDT.setLossRate(\cf3 0\cf0 );\
RDT.protocol = SR;\
\

\f1\b \cf2 for 
\f2\b0 \cf0 (
\f1\b \cf2 int 
\f2\b0 \cf0 i = \cf3 0\cf0 ; i < numMessages; i++) \{\
    
\f1\b \cf2 for 
\f2\b0 \cf0 (
\f1\b \cf2 int 
\f2\b0 \cf0 j = \cf3 0\cf0 ; j < messageSize; j++) \{\
        data[j] = (
\f1\b \cf2 byte
\f2\b0 \cf0 ) i;\
    \}\
    rdt.send(data, messageSize);\
\}\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 \
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0
\cf0 \
\
\
\
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f3\i \cf4 \
\
\pard\tx560\tx1120\tx1680\tx2240\tx2800\tx3360\tx3920\tx4480\tx5040\tx5600\tx6160\tx6720\pardirnatural\partightenfactor0
\cf4 \
\
\
\
\
}