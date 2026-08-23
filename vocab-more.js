// Additional Little Voices curriculum. Loaded after vocab.js.
(()=>{
const EXTRA="+json.dumps(extra, ensure_ascii=False,separators=(',',':'))+""";
const localeKey=l=>{const x=(VOCAB[l]?.locale||'').toLowerCase();return x.startsWith('en')?'English':x.startsWith('es')?'Spanish':x.startsWith('fr')?'French':x.startsWith('zh')||x.startsWith('cmn')?'Mandarin':l};
const hardPics=[
['👋','🙋','🆘','🤝','📍','🙏','💧','❤️'],
['🌅','😊','🍎','🚪','🚻','👨‍👩‍👧','😴','👋'],
['🍽️','🥤','❄️','☀️','😊','😢','😨','😴'],
['💧','🍎','👟','🎒','🔁','🤔','🐢','🙏'],
['⚽','🎮','📖','🏠','🚻','📚','📅','🙋'],
['🐶','⚽','🧑‍🤝‍🧑','🧑‍🏫','📖','🛒','💪','👋']
];
for(const l of Object.keys(VOCAB)){
  const v=VOCAB[l],k=localeKey(l),e=EXTRA[k];
  if(!e)continue;
  if(v.medium?.[0]?.[7]){const cake={English:'cake',Spanish:'pastel',French:'gâteau',Mandarin:'蛋糕'}[k]||'cake';v.medium[0][7].word=cake;v.medium[0][7].meaning='cake';v.medium[0][7].picture='🍰';}
  if(v.hard)for(let s=0;s<Math.min(6,v.hard.length);s++)for(let j=0;j<8;j++)if(v.hard[s][j])v.hard[s][j].picture=hardPics[s][j];
  for(const lev of ['easy','medium','hard']){
    const src=EXTRA.English[lev];
    const rows=e[lev].map((row,si)=>row.map(([text,picture],ji)=>lev==='hard'?{sentence:text,meaning:k==='English'?'':src[si][ji][0],picture}:{word:text,meaning:k==='English'?'':src[si][ji][0],picture}));
    v[lev].push(...rows);
  }
}
})();""" )}